package com.ljb.pojo.base;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseAuditable {

    @Column(name="id")
    @Id
    Long id;

    @LastModifiedDate
    @Column(name="updateTime")
    Date updateTime;

    @CreatedDate
    @Column(name="createTime")
    Date createTime;

    @Version
    @Column(name = "version")
    private Long version;
}
