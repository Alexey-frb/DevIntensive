package com.softdesign.devintensive.data.storage.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(active = true, nameInDb = "LIKES")
public class Likes {

    @Id
    private Long id;

    private String LikesBy;

    private String userRemoteId;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 683253613)
    private transient LikesDao myDao;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    public Likes(String likesBy, String userId) {
        LikesBy = likesBy;
        userRemoteId = userId;
    }

    @Generated(hash = 1895332607)
    public Likes(Long id, String LikesBy, String userRemoteId) {
        this.id = id;
        this.LikesBy = LikesBy;
        this.userRemoteId = userRemoteId;
    }

    @Generated(hash = 1083466601)
    public Likes() {
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1008259696)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLikesDao() : null;
    }

    public String getUserRemoteId() {
        return this.userRemoteId;
    }

    public void setUserRemoteId(String userRemoteId) {
        this.userRemoteId = userRemoteId;
    }

    public String getLikesBy() {
        return this.LikesBy;
    }

    public void setLikesBy(String LikesBy) {
        this.LikesBy = LikesBy;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
