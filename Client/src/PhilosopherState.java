public enum PhilosopherState {

    HUNGRY(1,2,10,5),NORMAL(1,5,10,3);

    public int getEatTime() {
        return eatTime;
    }

    public int getMeditateTime() {
        return meditateTime;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public int getMaxMealsEaten() {
        return maxMealsEaten;
    }

    private int eatTime;
    private int meditateTime;
    private int sleepTime;
    private int maxMealsEaten;
    PhilosopherState(int eatTime,int meditateTime, int sleepTime,int maxMealsEaten){
        this.eatTime = eatTime;
        this.meditateTime = meditateTime;
        this.sleepTime = sleepTime;
        this.maxMealsEaten = maxMealsEaten;
    }
}
