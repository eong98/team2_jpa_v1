package dev.jpa.allimio.shopcalendar;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShopCalendarDTO {

  private long no;
  private long sno;
  private int ctype;
  private String title;
  private String contents;
  private String sdate;
  private String edate;
  private String allday;
  private String color;
  private String status;
  private String cdate;
  private String udate;

  public ShopCalendarDTO() {

  }

  public ShopCalendarDTO(long no, long sno, int ctype, String title, String contents, String sdate,
      String edate, String allday, String color, String status, String cdate, String udate) {
    this.no = no;
    this.sno = sno;
    this.ctype = ctype;
    this.title = title;
    this.contents = contents;
    this.sdate = sdate;
    this.edate = edate;
    this.allday = allday;
    this.color = color;
    this.status = status;
    this.cdate = cdate;
    this.udate = udate;
  }

  public ShopCalendar toEntity() {
    return new ShopCalendar(no, sno, ctype, title, contents, sdate, edate, allday, color, status,
        cdate, udate);
  }

}
