import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Night {
    //private int id;
    private String start_date;
    private String end_date;
    private String sleep_time;
    private int apneaEventsNumber;

    public Night(int id, String start_date, String end_date) {
        //this.id = id;
        this.start_date = start_date;
        this.end_date = end_date;
        calculateSleepTime();
    }

    public Night(String start_date, String end_date, String sleep_time) {
        this.start_date = start_date;
        this.end_date = end_date;
        this.sleep_time = sleep_time;
    }

    public Night() {
        this.start_date = null;
        this.end_date = null;
        this.sleep_time = null;
    }

    public Night(String start_date, String end_date, String sleep_time, int apneaEventsNumber) {
        this.start_date = start_date;
        this.end_date = end_date;
        this.sleep_time = sleep_time;
        this.apneaEventsNumber = apneaEventsNumber;
    }

    public void reset () {
        //this.id = -1;
        this.start_date = null;
        this.end_date = null;
        this.sleep_time = null;
    }


    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getSleep_time() {
        return sleep_time;
    }

    public void setSleep_time(String sleep_time) {
        this.sleep_time = sleep_time;
    }

    public int getApneaEventsNumber() {
        return apneaEventsNumber;
    }

    public void setApneaEventsNumber(int apneaEventsNumber) {
        this.apneaEventsNumber = apneaEventsNumber;
    }

    public void calculateSleepTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date startDate = format.parse(start_date);
            Date endDate = format.parse(end_date);

            long diffMillis = endDate.getTime() - startDate.getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            // Calculate hours and minutes
            long hours = diffMinutes / 60;
            long minutes = diffMinutes % 60;

            // Format the result as "hh:mm"
            sleep_time = String.format("%02d:%02d", hours, minutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
