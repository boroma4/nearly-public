package com.ukrainianboyz.nearly.db.entity;


import com.ukrainianboyz.nearly.db.enums.Status;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@Entity
@Table(name = "\"UserRelationships\"", schema = "nearlyDB")
@NoArgsConstructor
@Data
@Builder
@IdClass(UserRelationshipPK.class)
public class UserRelationship {

    @Id
    @Column(name = "RequesterId")
    private String requesterId;

    @Id
    @Column(name = "ResponderId")
    private String responderId;

    @Column(name = "RequesterName")
    private String requesterName;

    @Column(name = "ResponderName")
    private String responderName;

    @Column(name = "Status")
    @Enumerated(EnumType.ORDINAL)
    private Status status;

    public String getOtherId(String id){
        return requesterId.equals(id) ? responderId : requesterId;
    }
}
