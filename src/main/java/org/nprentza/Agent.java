package org.nprentza;

public class Agent {
    private int id;
    private AgentRole role;    // admin, guest
    private String experience;
    private boolean grantAccess;

    public static Agent fromRawData(int id, String role, String experience){
        AgentRole agentRole = AgentRole.valueOf(role.toUpperCase());
        return new Agent(id, agentRole, experience);
    }

    private Agent(int id, AgentRole role, String experience){
        this.id = id;
        this.role = role;
        this.experience = experience;
    }

    public int getId(){return this.id;}

    public void setRole(AgentRole role){this.role=role;}
    public AgentRole getRole(){return this.role;}

    public void setExperience(String experience) {this.experience = experience;}
    public String getExperience(){return this.experience;}

    public void setGrantAccess(boolean access){this.grantAccess=access;}
    public boolean getGrandAccess(){return this.grantAccess;}
}
