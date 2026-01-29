package com.swd392.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
@Tag(name = "Hello", description = "Test endpoint")
public class HelloController {

    @Operation(
            summary = "Say Hello",
            description = "A simple test endpoint that returns a greeting message. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping("/")
    public String sayHello() {
        return "Hello, World!";
    }
}
