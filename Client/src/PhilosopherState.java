public enum PhilosopherState {

    HUNGRY(1,2,10,5,false),NORMAL(1,5,10,3,false);

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

    public boolean isBanned(){
        return banned;
    }
    public void setBanned(boolean val){
        banned = val;
    }

    private boolean banned;
    private int eatTime;
    private int meditateTime;
    private int sleepTime;
    private int maxMealsEaten;
    PhilosopherState(int eatTime,int meditateTime, int sleepTime,int maxMealsEaten, boolean banned){
        this.eatTime = eatTime;
        this.meditateTime = meditateTime;
        this.sleepTime = sleepTime;
        this.maxMealsEaten = maxMealsEaten;
        this.banned = banned;
    }
}
