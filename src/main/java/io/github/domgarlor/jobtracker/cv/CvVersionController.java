package io.github.domgarlor.jobtracker.cv;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only on purpose: there is no PUT or DELETE because a version must never change once it may have been sent.
 */
@RestController
@RequestMapping("/api/cv-versions")
class CvVersionController {

	private final CvService service;

	CvVersionController(CvService service) {
		this.service = service;
	}

	@GetMapping("/{id}")
	CvVersionResponse get(@PathVariable long id) {
		return CvVersionResponse.from(service.getVersion(id));
	}

	@GetMapping("/{id}/file")
	ResponseEntity<Resource> download(@PathVariable long id) {
		CvVersion version = service.getVersion(id);
		ContentDisposition disposition = ContentDisposition.attachment()
			.filename(version.getOriginalFilename(), StandardCharsets.UTF_8)
			.build();
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
			.body(service.loadFile(version));
	}

}
