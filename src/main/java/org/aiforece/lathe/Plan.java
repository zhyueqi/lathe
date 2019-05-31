package org.aiforece.lathe;

/**
 * Created by zhyueqi on 2019/5/31.
 */
public class Plan {
    public static final String PLAN_SUM = "PLAN_SUM";
    public static final String PLAN_GEN = "PLAN_GEN";
    public static final String SUBPLAN = "SUBPLAN";
    public String body;
    public String plan;
    public Plan(String body, String planName){
        this.body = body;
        this.plan = planName;
    }

    public Plan(String body){
        this.plan = body.split(",")[0];
        this.body = body.split(",")[1];
    }

    public String toString(){
        return this.plan + "," + this.body;
    }
    // to-do : extract interface
    public String execute_plan_gen(){
        int x = Integer.parseInt(this.body);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(SUBPLAN + "@");
        for(int i=0; i< x; i++){
            if(i != 0)
                stringBuffer.append(";");
            stringBuffer.append(PLAN_SUM + "," + i );
        }
        return stringBuffer.toString();
    }
    // to-do : extract interface
    public String execute_plan_sum(){
        int x = Integer.parseInt(this.body);
        return x + 1 + " [RESULT!]";
    }
}
