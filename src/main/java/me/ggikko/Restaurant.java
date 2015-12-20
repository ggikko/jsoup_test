package me.ggikko;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Created by Park Ji Hong, ggikko.
 */
@Data
@AllArgsConstructor
@ToString
public class Restaurant {
    private String number;
    private String name;
    private String kind;
    private String addr;
    private String addrs;
}
