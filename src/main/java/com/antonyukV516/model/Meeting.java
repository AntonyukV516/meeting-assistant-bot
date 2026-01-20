package com.antonyukV516.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Column(name = "title", nullable = false, length = 50)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @ElementCollection(targetClass = Tag.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meeting_tags",
            joinColumns = @JoinColumn(name = "meeting_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private List<Tag> tags = new ArrayList<>();

    @Column(name = "date_time")
    LocalDateTime dateTime;

    @Column(name = "location", length = 100)
    String location;

    @Column(name = "max_people")
    Integer maxPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_username", referencedColumnName = "telegram_username", nullable = false)
    User creator;
}