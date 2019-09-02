package kr.co.shineware.nlp.komoran.admin.domain;

import javax.persistence.*;

@Entity
@Table(name = "FWDIC",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fullwords"})
        })
public class FwdUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "fullwords")
    private String full;

    @Column(name = "analyzed")
    private String analyzed;

    public int getId() {
        return id;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(String analyzed) {
        this.analyzed = analyzed;
    }

    @Override
    public String toString() {
        return "FwdUser{" +
                "id=" + id +
                ", full='" + full + '\'' +
                ", analyzed='" + analyzed + '\'' +
                '}';
    }

    public FwdUser() {
    }

    public FwdUser(String full, String analyzed) {
        this.full = full;
        this.analyzed = analyzed;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FwdUser) {
            FwdUser newItem = (FwdUser) obj;
            if (this.full.equals(newItem.getFull()) && (this.analyzed.equals(newItem.getAnalyzed()))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((full == null) ? 0 : full.hashCode());
        result = prime * result + ((analyzed == null) ? 0 : analyzed.hashCode());

        return result;
    }
}
