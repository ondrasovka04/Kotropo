package cz.ucenislovicek.Adapter;

public class Subject {
    private final boolean[] week;
    private final String name;



    public Subject(String name) {
        this.week = new boolean[7];
        this.name = name;
    }

    public boolean[] getWeek() {
        return week;
    }

    public String getName() {
        return name;
    }
    public void setDay(int index){
        week[index] = true;
    }
}
