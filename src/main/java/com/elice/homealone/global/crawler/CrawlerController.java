package com.elice.homealone.global.crawler;

import com.elice.homealone.global.jobstatus.JobStatus;
import com.elice.homealone.global.jobstatus.JobStatusService;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.MemberQueryService;
import com.elice.homealone.module.member.service.MemberService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final MemberService memberService;
    private final JobStatusService jobStatusService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/saverecipe/{date}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> importRecipeData(@AuthenticationPrincipal Member member, @PathVariable String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date parsedDate;
        try {
            parsedDate = formatter.parse(date);
        } catch (ParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String jobId = UUID.randomUUID().toString();
        crawlerService.loadFromMongoAndSaveRecipe(memberQueryService.findById(member.getId()), parsedDate, jobId);
        return ResponseEntity.accepted().body(jobId);
    }

    @GetMapping("/jobstatus/{jobId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<JobStatus> getJobStatus(@PathVariable String jobId) {
        JobStatus jobStatus = jobStatusService.getJobStatus(jobId);
        if (jobStatus == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobStatus);
    }
}
