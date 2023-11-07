**DRL rules from data (Drools-ML integration)**

[DROOLS-7572](https://issues.redhat.com/browse/DROOLS-7572)

The idea is to combine domain expert's knowledge, that is available in a symbolic form (DRL rules), with rules that can be derived from data, using a machine learning algorithm, 
to build a more comprehensive Drools application.

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


