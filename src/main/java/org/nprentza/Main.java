package org.nprentza;

import emla.dbcomponent.Dataset;
import emla.learning.LearningSession;
import emla.learning.oner.Frequency;
import emla.learning.oner.FrequencyTable;

import java.util.List;

public class Main {

    /*
        validate a DRL  against a set of data to identify any gaps
     */
    public static void main(String[] args) {

        DroolsAgentApp agentApp = new DroolsAgentApp();
        //  load data from a csv file
        Dataset ds = new Dataset("./src/main/resources/agentRequests.csv", "resourceAccess", "access", 1, 0);
        //  create Agent objects from data
        agentApp.loadAgentsFromData(ds);
        //  evaluate DRL
        DrlAssessment assessment = agentApp.evaluateAgentRequests();
        //  if coverage is less than 100% then we need to find additional rules for data (agent objects) not covered
        if (assessment.getCoverage()<1){
            System.out.println("Coverage is less than 100%. \nUse OneR algorithm to find rules for data not covered.");
            LearningSession emlaSession = new LearningSession(ds,"agentsApp");
            List<Integer> casesNotCovered = agentApp.getRequestsNotCovered();
            List<FrequencyTable> frequencyTables = emlaSession.calculateFrequencyTables(ds, "train",casesNotCovered);
            frequencyTables.forEach(ft -> System.out.println(ft.toString()));
            Frequency fHighCovLowError = emlaSession.calculateFrequencyHighCoverageLowError(frequencyTables);
            System.out.println("\nUpdate DRL with the *best* frequency selected:" + fHighCovLowError.toString());
            agentApp.updateDrl(frequencyTables, fHighCovLowError);
            //  repeat evaluation
            System.out.println("\nRe-evaluate the DRL.");
            assessment = agentApp.evaluateAgentRequests();
            System.out.println("The coverage of the revised DRL is " + assessment.getCoverage()*100 + "%.");
        }else{
            System.out.println("Coverage is 100%.");
        }

    }
}
