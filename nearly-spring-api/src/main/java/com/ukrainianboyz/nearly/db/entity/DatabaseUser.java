package com.ukrainianboyz.nearly.db.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Entity
@Table(name = "\"Users\"", schema = "nearlyDB")
@NoArgsConstructor
@Data
public class DatabaseUser {

    @Column(name = "UserId")
    @Id
    String userId;

    @Column(name = "UserName")
    String userName;

    @Column(name = "UserBio")
    String userBio;

    @Column(name = "Email")
    String email;

    @Column(name = "ImageUrl")
    String imageUrl;

    @Column(name = "StatusIndicator")
    Integer statusIndicator;

    @Column(name = "AppUserId")
    String appUserId;

}
