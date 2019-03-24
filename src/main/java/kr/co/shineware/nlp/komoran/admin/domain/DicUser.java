package kr.co.shineware.nlp.komoran.admin.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "DICUSER",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"token", "pos"})
})
public class DicUser implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "token")
    private String token;

    @Column(name = "pos")
    @Enumerated(EnumType.STRING)
    private PosType pos;


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

    @Override
    public String toString() {
        return "DicUser{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", pos='" + pos + '\'' +
                '}';
    }

    public DicUser() { }

    public DicUser(String token, PosType pos) {
        this.token = token;
        this.pos = pos;
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof DicUser){
            DicUser newItem = (DicUser) obj;
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
