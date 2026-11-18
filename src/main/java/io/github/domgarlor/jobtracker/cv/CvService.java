package io.github.domgarlor.jobtracker.cv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.github.domgarlor.jobtracker.shared.ConflictException;
import io.github.domgarlor.jobtracker.shared.NotFoundException;

@Service
@Transactional(readOnly = true)
public class CvService {

	private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

	private static final String DEFAULT_FILENAME = "cv.pdf";

	private final CvProfileRepository profiles;

	private final CvVersionRepository versions;

	private final CvFileStorage storage;

	private final Clock clock;

	CvService(CvProfileRepository profiles, CvVersionRepository versions, CvFileStorage storage, Clock clock) {
		this.profiles = profiles;
		this.versions = versions;
		this.storage = storage;
		this.clock = clock;
	}

	@Transactional
	public CvProfile createProfile(String name, String description) {
		String trimmedName = name.strip();
		if (profiles.existsByNameIgnoreCase(trimmedName)) {
			throw new ConflictException("A CV profile named '%s' already exists".formatted(trimmedName));
		}
		return profiles.save(new CvProfile(trimmedName, description, Instant.now(clock)));
	}

	public List<CvProfile> listProfiles() {
		return profiles.findAllByOrderByNameAsc();
	}

	public CvProfile getProfile(long id) {
		return profiles.findById(id).orElseThrow(() -> new NotFoundException("CV profile %d not found".formatted(id)));
	}

	@Transactional
	public CvVersion addVersion(long profileId, MultipartFile file, String notes) {
		CvProfile profile = getProfile(profileId);
		byte[] content = readContent(file);
		if (!isPdf(content)) {
			throw new UnsupportedCvFileException("The file is not a PDF");
		}

		String sha256 = sha256(content);
		versions.findBySha256(sha256).ifPresent(existing -> {
			throw new DuplicateCvVersionException(existing.getId());
		});

		// The key is the hash, so the same content can never be stored twice under different names.
		String storageKey = sha256 + ".pdf";
		storage.store(storageKey, content);

		return versions.save(new CvVersion(profile, safeFilename(file.getOriginalFilename()), content.length, sha256,
				storageKey, notes, Instant.now(clock)));
	}

	public List<CvVersion> listVersions(long profileId) {
		getProfile(profileId);
		return versions.findByProfileIdOrderByUploadedAtDescIdDesc(profileId);
	}

	public CvVersion getVersion(long id) {
		return versions.findById(id).orElseThrow(() -> new NotFoundException("CV version %d not found".formatted(id)));
	}

	public Resource loadFile(CvVersion version) {
		return storage.load(version.getStorageKey());
	}

	private static byte[] readContent(MultipartFile file) {
		try {
			return file.getBytes();
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Cannot read uploaded file", ex);
		}
	}

	private static boolean isPdf(byte[] content) {
		return content.length >= PDF_SIGNATURE.length
				&& Arrays.equals(content, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length);
	}

	private static String sha256(byte[] content) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	/** Keeps only the file name: a client could send a path such as {@code ../../etc/passwd}. */
	private static String safeFilename(String originalFilename) {
		String filename = StringUtils.getFilename(StringUtils.cleanPath(String.valueOf(originalFilename)));
		return StringUtils.hasText(filename) && !"null".equals(filename) ? filename : DEFAULT_FILENAME;
	}

}
