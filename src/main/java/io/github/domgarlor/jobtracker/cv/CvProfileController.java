package io.github.domgarlor.jobtracker.cv;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/cv-profiles")
class CvProfileController {

	private final CvService service;

	CvProfileController(CvService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<CvProfileResponse> create(@Valid @RequestBody CreateCvProfileRequest request) {
		CvProfile profile = service.createProfile(request.name(), request.description());
		return ResponseEntity.created(URI.create("/api/cv-profiles/" + profile.getId()))
			.body(CvProfileResponse.from(profile));
	}

	@GetMapping
	List<CvProfileResponse> list() {
		return service.listProfiles().stream().map(CvProfileResponse::from).toList();
	}

	@GetMapping("/{id}")
	CvProfileResponse get(@PathVariable long id) {
		return CvProfileResponse.from(service.getProfile(id));
	}

	@PostMapping(path = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<CvVersionResponse> uploadVersion(@PathVariable long id, @RequestPart("file") MultipartFile file,
			@RequestParam(required = false) @Size(max = 500) String notes) {
		CvVersion version = service.addVersion(id, file, notes);
		return ResponseEntity.created(URI.create("/api/cv-versions/" + version.getId()))
			.body(CvVersionResponse.from(version));
	}

	@GetMapping("/{id}/versions")
	List<CvVersionResponse> listVersions(@PathVariable long id) {
		return service.listVersions(id).stream().map(CvVersionResponse::from).toList();
	}

	record CreateCvProfileRequest(@NotBlank @Size(max = 60) String name, @Size(max = 255) String description) {
	}

}
