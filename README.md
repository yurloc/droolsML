# DRL rules from data (Drools-ML integration)

[DROOLS-7572](https://issues.redhat.com/browse/DROOLS-7572)

The idea is to combine domain expert's knowledge :one:, that is available in a symbolic form (DRL rules), with rules that can be derived from data :two:, using a machine learning algorithm :three:, 
to build a more comprehensive Drools application :four:.

## Jiri's summary

### :one: Expert knowledge
These are the rules that we already have.
They come from the domain knowledge and the historical data.
```
rule 'AllowAdmin' when
    $a: Agent( role == AgentRole.ADMIN )
then
    $a.setGrantAccess( true );
end

rule 'DenyGuest' when
    $a: Agent( role == AgentRole.GUEST )
then
    $a.setGrantAccess( false );
end
```

### :two: Data

|  # | role        | experience | age | access |      covered       |
|---:|-------------|------------|----:|--------|:------------------:|
|  1 | admin       | senior     |  38 | allow  | :white_check_mark: |
|  2 | guest       | junior     |  28 | deny   | :white_check_mark: |
|  3 | contributor | junior     |  32 | allow  |        :x:         |
|  4 | admin       | senior     |  45 | allow  | :white_check_mark: |
|  5 | guest       | senior     |  44 | deny   | :white_check_mark: |
|  6 | contributor | senior     |  42 | allow  |        :x:         |
|  7 | guest       | junior     |  18 | deny   | :white_check_mark: |

> [!NOTE]  
> Note that based on the rules we already have :one:, 5 out of 7 data points are covered (they are granted or denied access in the RHS). Data points 3 and 6 are **not covered**.


### :three: ML-discovered rules

Best result from the ML algorithm's output:
```
>> for role=contributor (Coverage= 1 )  (Best target value = allow, with error=0 )  ) :
	[allow, 2]
```
It basically says: The condition `role = contributor` predicts the target value (`access = allow`) for 2 uncovered data points (100% coverage) and it has 0 errors, meaning it doesn't erroneously override any decision of the existing rules.

We transform this information into DRL and add the new rule to the knowledge base:

```
rule 'allowcontributor' when
    $a: Agent( role == AgentRole.CONTRIBUTOR ) 
then
    $a.setGrantAccess( true );
end
```

### :four: Validation and verification

Before making the final step and adding the ML-discovered rule into the knowledge base, we want to use verification & validation tools to **confirm** the validity of the new rule.

In this simple example, we want the tool to report that there is a "gap" in the existing rules that doesn't cover the whole range of the `AgentRole` enumeration.

We would like to compare the new rule with the verification report and see that it fits into the "contributor" gap.

## Issues we ran into with drools-verifier

Current drools-verifier report says:
```
===== NOTES =====
===== WARNS =====
Warning id = 0:
faulty : LiteralRestriction from rule [AllowAdmin] value '== AgentRole.ADMIN'
Rule base covers == AgentRole.ADMIN, but it is missing != AgentRole.ADMIN 
	Cause trace: 

Warning id = 1:
faulty : LiteralRestriction from rule [DenyGuest] value '== AgentRole.GUEST'
Rule base covers == AgentRole.GUEST, but it is missing != AgentRole.GUEST 
	Cause trace: 

===== ERRORS =====
===== GAPS =====
Gap: (Field 'age' from object type 'Agent') Operator = '>=' 19 from rule: [DenyChildren]
```

Problems:
1. Range checks do not support categorical data (enums).
2. Rules are evaluated individually.

The report tells me that each rule is missing the opposite side of the check (e.g. `!= ADMIN`).
How does this help me decide whether the `== CONTRIBUTOR` is a good addition to the existing rule base?
Instead of that I'd like to see something like this:
```
Rule base covers AgentRole.ADMIN and AgentRole.GUEST but it is missing '== AgentRole.CONTRIBUTOR'.
```

The gap analysis works better for integer fields but again, it doesn't combine multiple rules.
It only reports the opposite side of the range check for each rule individually.

## Nicole's original description

The repo currently includes a very basic example that shows how a DRL can be validated against a given set of data,
and how the [emla](https://github.com/nprentza/emla) libray can be used to discover additional rules for cases not covered by the DRL (if any).
These rules can be combined with the initial DRL for the development of a more comprehensive Drools application. 
Incorporation of the new rules into the existing DRL is still under investigation.

The `DroolsAgentApp` example:
- The initial DRL contains the following rules:
```
rule AllowAdmin when
	$a: Agent( role == "admin" )
then
    $a.setGrantAccess( true );
	allow.add( $a.getId() );
end
rule DenyGuest when
	$a: Agent( role == "guest" )
then
    $a.setGrantAccess( false );
	deny.add( $a.getId() );
end
```
- A csv file contains 6 entries of agent request: 2 requests from `admin` agents, 2 requests from `guest` cases and 2 requests from `contributor` cases.
- Data from the csv file are used to create `Agent` objects.
- To validate the initial DRL:
```Java
 for (Agent a : agentRequests){
    kieSession.insert(a);
 }
 kieSession.fireAllRules();
 if ( (allow.size()+deny.size()) < agentRequests.size()) {
        // identify the cases not covered
        // use emla to find additional rules for these cases
        // revise the initial DRL
        // repeat the validation process
 }
```
- The validation shows that the initial DRL cannot support the 2 `contributor` requests. 
We can then use `emla` to derive a rule for these requests from the 2 dataset entries and add i.e. the following to the initial DRL:
```
rule AllowContributor when
	$a: Agent( role == "contributor" )
then
    $a.setGrantAccess( true );
	allow.add( $a.getId() );
end
```
- Now the DRL contains three rules. 
- If we repeat the validation process all `Agent` requests will be added in one of the lists, `allow` or `deny`.At this point we consider that the DRL fully covers the given dataset. 


