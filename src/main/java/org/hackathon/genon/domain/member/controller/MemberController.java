package org.hackathon.genon.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.controller.dto.MemberCreateRequest;
import org.hackathon.genon.domain.member.service.MemberCommandService;
import org.hackathon.genon.domain.member.service.MemberQueryService;
import org.hackathon.genon.domain.member.service.dto.MemberProfileResponse;
import org.hackathon.genon.global.annotation.AuthMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController extends MemberDocsController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @Override
    @PostMapping("/v1/members/register")
    public ResponseEntity<Void> create(@Valid @RequestBody MemberCreateRequest request) {
        memberCommandService.register(request.toCreateProfile());

        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/v1/members/profile")
    public ResponseEntity<MemberProfileResponse> getProfile(@AuthMember Long memberId) {
        MemberProfileResponse response = memberQueryService.getProfile(memberId);

        return ResponseEntity.ok(response);
    }

}