package com.elice.homealone.global.crawler;

import com.elice.homealone.global.jobstatus.JobStatus;
import com.elice.homealone.global.jobstatus.JobStatusService;
import com.elice.homealone.module.login.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.entity.Role;
import com.elice.homealone.module.member.repository.MemberRepository;
import com.elice.homealone.module.recipe.dto.RecipeRequestDto;
import com.elice.homealone.module.recipe.service.RecipeService;

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final RecipeService recipeService;
    private final RecipeMongoRepository recipeMongoRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobStatusService jobStatusService;

    /*
        public void loadJsonAndSaveRecipe() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // JSON 파일을 읽어와서 RecipeRequestDto 형태로 변환
                List<RecipeRequestDto> recipeRequestDtos = objectMapper.readValue(new File("/data/recipe.json"), new TypeReference<List<RecipeRequestDto>>(){});

                for(RecipeRequestDto requestDto : recipeRequestDtos) {
                    recipeService.createRecipe(requestDto);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

     */
    @Async
    public void loadFromMongoAndSaveRecipe(Member member, Date date, String jobId) {
        JobStatus jobStatus = jobStatusService.createJobStatus(jobId);
        // MongoDB에서 Recipe 데이터를 읽어옴
        List<RecipeRequest> recipes = recipeMongoRepository.findAllWithCreatedDateAfter(date);

        int totalRecipes = recipes.size();
        int processCount = 0;
        for(RecipeRequest recipe : recipes) {
            // 변환된 RecipeRequestDto를 사용하여 레시피 생성
            RecipeRequestDto requestDto = RecipeRequestDto.from(recipe);
            recipeService.createRecipe(member, requestDto);
            processCount++;
            if(processCount % (totalRecipes / 10) == 0){
                int progress = (int) ((double) processCount / totalRecipes * 100);
                jobStatusService.updateJobStatusProgress(jobId, progress);
            }
        }
        jobStatusService.markJobAsCompleted(jobId);
    }

    public void adminSignUp(SignupRequestDto signupRequestDTO) {
        String email = signupRequestDTO.getEmail();
        if (!memberRepository.existsByEmail(email)) {
            Member admin = Member.builder()
                .name("관리자")
                .birth(signupRequestDTO.getBirth()) // 관리자 생년월일 예시
                .email(email)
                .firstAddress(signupRequestDTO.getFirstAddress())
                .secondAddress(signupRequestDTO.getSecondAddress())
                .password(passwordEncoder.encode(signupRequestDTO.getPassword())) // 관리자 비밀번호
                .role(Role.ROLE_ADMIN) // 관리자 역할 설정
                .build();
            memberRepository.save(admin);
        }
    }
}
