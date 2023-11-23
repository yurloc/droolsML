package org.nprentza;

import org.emla.learning.Frequency;

public final class DrlConverter {

    private DrlConverter() {
    }

    public static String frequencyToDrlRule(Frequency frequency) {
        String field = frequency.getPredictorValues().getLeft();
        String value = frequency.getPredictorValues().getRight().toString();
        String decision = frequency.getBestTargetValue();
        String ruleName = decision + value;

        return rule(ruleName, field, value, decision);
    }

    public static String preamble() {
        return "package org.nprentza;\n" +
                "\n" +
                "import " + Agent.class.getCanonicalName() + ";\n" +
                "import " + AgentRole.class.getCanonicalName() + ";\n" +
                "\n" +
                "global java.util.List allow;\n" +
                "global java.util.List deny;\n" +
                "\n";
    }

    public static String rule(String ruleName, String field, String value, String decision) {
        return "rule '" + ruleName + "' when\n" +
                "  $a: Agent( " + equalityCheck(field, value) + " ) \n" +
                "then\n" +
                "  $a.setGrantAccess( " + grantAccess(decision) + " );\n" +
                "  " + decision + ".add( $a.getId() );\n" +
                "end\n";
    }

    private static String equalityCheck(String field, String value) {
        if (field.equals("role")) {
            return "role == " + AgentRole.class.getSimpleName() + "." + AgentRole.valueOf(value.toUpperCase()).name();
        }
        return field + " == " + value;
    }

    private static boolean grantAccess(String decision) {
        return decision.equals("allow");
    }
}
