package io.github.domgarlor.jobtracker.cv;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One exact PDF of a profile. It is immutable on purpose: an application must always point to the very file that was
 * sent, so a changed CV is uploaded as a new version instead of editing an old one.
 */
@Entity
@Immutable
@Table(name = "cv_version")
public class CvVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "profile_id", nullable = false)
	private CvProfile profile;

	@Column(name = "original_filename", nullable = false)
	private String originalFilename;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(nullable = false, length = 64)
	private String sha256;

	@Column(name = "storage_key", nullable = false, length = 100)
	private String storageKey;

	@Column(length = 500)
	private String notes;

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	protected CvVersion() {
		// required by JPA
	}

	public CvVersion(CvProfile profile, String originalFilename, long sizeBytes, String sha256, String storageKey,
			String notes, Instant uploadedAt) {
		this.profile = profile;
		this.originalFilename = originalFilename;
		this.sizeBytes = sizeBytes;
		this.sha256 = sha256;
		this.storageKey = storageKey;
		this.notes = notes;
		this.uploadedAt = uploadedAt;
	}

	public Long getId() {
		return id;
	}

	public CvProfile getProfile() {
		return profile;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public String getSha256() {
		return sha256;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getUploadedAt() {
		return uploadedAt;
	}

}
