package me.rfprojects.clusterdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Node implements Comparable<Node> {

    private int term;
    private String address;
    private int port;

    @Override
    public int compareTo(Node that) {
        return this.term - that.term;
    }
}
