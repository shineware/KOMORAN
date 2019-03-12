package kr.co.shineware.nlp.komoran.admin.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "DICWORD",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"token", "pos"})
})
public class DicWord implements Serializable {
    @Id
    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MY_ENTITY_SEQ")
    @SequenceGenerator(name = "MY_ENTITY_SEQ", sequenceName = "MY_ENTITY_SEQ", allocationSize = 200)
    private int id;

    @Column(name = "token")
    private String token;

    @Column(name = "pos")
    @Enumerated(EnumType.STRING)
    private PosType pos;

    @Column(name = "tf")
    private int tf;


    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PosType getPos() {
        return pos;
    }

    public void setPos(PosType pos) {
        this.pos = pos;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    @Override
    public String toString() {
        return "DicWord{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", pos='" + pos + '\'' +
                ", tf=" + tf +
                '}';
    }

    public DicWord() { }

    public DicWord(String token, PosType pos, int tf) {
        this.token = token;
        this.pos = pos;
        this.tf = tf;
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof DicWord){
            DicWord newItem = (DicWord) obj;
            if (this.token.equals(newItem.getToken()) && (this.pos == newItem.getPos())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((pos == null) ? 0 : pos.hashCode());
        return result;
    }

}
