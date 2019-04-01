package kr.co.shineware.nlp.komoran.admin.domain;

import antlr.Grammar;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GRAMMARIN",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"start", "next"})
        })
public class GrammarIn implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "start")
    @Enumerated(EnumType.STRING)
    private GrammarType start;

    @Column(name = "next")
    @Enumerated(EnumType.STRING)
    private GrammarType next;

    @Column(name = "tf")
    private int tf;


    public int getId() {
        return id;
    }

    public GrammarType getStart() {
        return start;
    }

    public void setStart(GrammarType start) {
        this.start = start;
    }

    public GrammarType getNext() {
        return next;
    }

    public void setNext(GrammarType next) {
        this.next = next;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    @Override
    public String toString() {
        return "GrammarIn{" +
                "id=" + id +
                ", start=" + start +
                ", next=" + next +
                ", tf=" + tf +
                '}';
    }

    public GrammarIn() {
    }

    public GrammarIn(GrammarType start, GrammarType next, int tf) {
        this.start = start;
        this.next = next;
        this.tf = tf;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof GrammarIn) {
            GrammarIn newItem = (GrammarIn) obj;
            if (this.start == newItem.getStart() && this.next == newItem.getNext() && this.tf == newItem.getTf()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());

        return result;
    }

}
