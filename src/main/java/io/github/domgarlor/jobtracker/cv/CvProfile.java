package io.github.domgarlor.jobtracker.cv;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A family of CVs aimed at one kind of job (QA, Backend .NET, BBDD...). Profiles are open-ended: new ones can be
 * created at any time.
 */
@Entity
@Table(name = "cv_profile")
public class CvProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CvProfile() {
		// required by JPA
	}

	public CvProfile(String name, String description, Instant createdAt) {
		this.name = name;
		this.description = description;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
