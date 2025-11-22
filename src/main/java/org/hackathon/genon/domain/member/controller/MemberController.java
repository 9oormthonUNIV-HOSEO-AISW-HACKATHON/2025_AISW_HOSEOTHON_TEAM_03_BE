package org.hackathon.genon.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.controller.dto.MemberCreateRequest;
import org.hackathon.genon.domain.member.service.MemberCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController extends MemberDocsController {

    private final MemberCommandService memberCommandService;

    @Override
    @PostMapping("/v1/members")
    public ResponseEntity<Void> create(@Valid @RequestBody MemberCreateRequest request) {
        memberCommandService.register(request.toCreateProfile());

        return ResponseEntity.ok().build();
    }

}