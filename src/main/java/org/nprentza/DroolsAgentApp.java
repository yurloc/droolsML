package org.nprentza;

import org.emla.dbcomponent.Dataset;
import org.emla.learning.Frequency;
import org.emla.learning.FrequencyTable;
import org.drools.model.codegen.ExecutableModelProject;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.List;

public class DroolsAgentApp {

    private KieBase kieBase;
    private List<Agent> agentRequests;
    private List<Integer> allow;
    private List<Integer> deny;

    private String DRL =
            "import " + Agent.class.getCanonicalName() + ";" +
                    "global java.util.List allow;" +
                    "global java.util.List deny;" +
                    "rule 'AllowAdmin' when\n" +
                    "  $a: Agent( role == 'admin' ) \n" +
                    "then\n" +
                    "  $a.setGrantAccess( true );\n" +
                    "  allow.add( $a.getId() );\n" +
                    "end\n" +
                    "rule 'DenyGuest' when\n" +
                    "  $a: Agent( role == 'guest' ) \n" +
                    "then\n" +
                    "  $a.setGrantAccess( false );\n" +
                    "  deny.add( $a.getId() );\n" +
                    "end";

    public DroolsAgentApp(){
        //kieBase = new KieHelper().addFromClassPath("/dataAccess.drl").build(ExecutableModelProject.class);
        kieBase = new KieHelper().addContent(DRL, ResourceType.DRL).build(ExecutableModelProject.class);
    }

    public void updateDrl(List<FrequencyTable> frequencyTables, Frequency bestFrequency){
        /*
            if bestFrequency condition is not already in the drl then add it
            else    process frequencyTables to find the next best frequency and repeat the process
         */
        DRL += "\n" + FrequencyToDrlRule(bestFrequency);
        kieBase = new KieHelper().addContent(DRL, ResourceType.DRL).build(ExecutableModelProject.class);
    }

    public void loadAgentsFromData(Dataset ds){
        this.agentRequests = new ArrayList<>();

        for (int i=0; i<ds.getRowCout(); i++) {
            this.agentRequests.add(new Agent(ds.getDsTable().row(i).getInt("caseID"),
                    ds.getDsTable().row(i).getString("role"),
                    ds.getDsTable().row(i).getString("experience")));
        }
    }

    public DrlAssessment evaluateAgentRequests(){
        KieSession kieSession = kieBase.newKieSession();
        allow = new ArrayList<>();
        kieSession.setGlobal("allow", allow);
        deny = new ArrayList<>();
        kieSession.setGlobal("deny", deny);

        for (Agent a : agentRequests){
            kieSession.insert(a);
        }

        kieSession.fireAllRules();

        // return the results of DRL's assessment
        DrlAssessment assessment = new DrlAssessment((double)(allow.size() + deny.size()) / agentRequests.size());
        return assessment;
    }

    public List<Integer> getRequestsNotCovered(){
        List<Integer> caseIdsNotCovered = new ArrayList<>();

        for (Agent a : agentRequests){
            if (!this.deny.contains(a.getId()) && !this.allow.contains(a.getId())){
                caseIdsNotCovered.add(a.getId());
            }
        }

        return caseIdsNotCovered;
    }

    private String FrequencyToDrlRule(Frequency frequency){
        String rule = " rule ";
        rule += "'" + frequency.getBestTargetValue() + frequency.getPredictorValues().getRight() + "' when \n";
        rule += "       $a: Agent( " + frequency.getPredictorValues().getLeft() + " == '" + frequency.getPredictorValues().getRight() + "' )\n" +
                "   then\n" +
                "       $a.setGrantAccess( " + (frequency.getBestTargetValue().equals("allow") ? " true " : " false ") + "); \n";
        if (frequency.getBestTargetValue().equals("allow")) {
            rule += "       allow.add( $a.getId() ); \n";
        }else {
            rule += "       deny.add( $a.getId() ); \n";
        }
        rule += "end";
        return rule;
    }
}
