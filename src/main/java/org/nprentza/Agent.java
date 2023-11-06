package org.nprentza;

public class Agent {
    private int id;
    private String role;    // admin, guest
    private String experience;
    private boolean grantAccess;

    public Agent(int id, String role, String experience){
        this.id = id;
        this.role = role;
        this.experience = experience;
    }

    public int getId(){return this.id;}

    public void setRole(String role){this.role=role;}
    public String getRole(){return this.role;}

    public void setExperience(String experience) {this.experience = experience;}
    public String getExperience(){return this.experience;}

    public void setGrantAccess(boolean access){this.grantAccess=access;}
    public boolean getGrandAccess(){return this.grantAccess;}
}
