package dev.jpa.allimio.shopcalendar;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ShopCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_calendar_seq_use")
  @SequenceGenerator(name = "shop_calendar_seq_use", sequenceName = "SEQ_SHOP_CALENDAR_NO", allocationSize = 1)
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

  public ShopCalendar() {

  }

  public ShopCalendar(long no, long sno, int ctype, String title, String contents, String sdate,
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

}
