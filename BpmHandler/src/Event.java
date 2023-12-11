public class Event {
    //private int id;
    private double bpm;
    private String date;
    private String type;
    private int night;

    public Event(int id, double bpm, String date, String type, int night) {
        //this.id = id;
        this.bpm = bpm;
        this.date = date;
        this.type = type;
        this.night = night;
    }

    public Event(double bpm, String date, String type, int night) {
        this.bpm = bpm;
        this.date = date;
        this.type = type;
        this.night = night;
    }

    public Event(double bpm, String date, int night) {
        this.bpm = bpm;
        this.date = date;
        this.type = null;
        this.night = night;
    }

    public Event() {
        //this.id = -1;
        this.bpm = -1;
        this.date = null;
        this.type = null;
        this.night = -1;
    }

    public void reset(){
        //this.id = -1;
        this.bpm = -1;
        this.date = null;
        this.type = null;
        this.night = -1;
    }


    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNight() {
        return night;
    }

    public void setNight(int night) {
        this.night = night;
    }


}
