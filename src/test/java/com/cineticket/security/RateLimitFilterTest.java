package com.cineticket.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    @Test
    void blocksTheEleventhLoginAttemptFromTheSameIpWithinTheWindow() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(loginRequest(), response, chain);
            assertEquals(200, response.getStatus());
        }
        verify(chain, times(10)).doFilter(any(), any());

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest(), blockedResponse, chain);

        assertEquals(429, blockedResponse.getStatus());
        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    void doesNotThrottleADifferentIpOnTheSameEndpoint() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(loginRequest("10.0.0.1"), response, chain);
            assertEquals(200, response.getStatus());
        }

        MockHttpServletResponse otherIpResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest("10.0.0.2"), otherIpResponse, chain);

        assertEquals(200, otherIpResponse.getStatus());
    }

    @Test
    void doesNotThrottleEndpointsOutsideTheConfiguredList() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/movies");
            request.setRemoteAddr("10.0.0.5");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertEquals(200, response.getStatus());
        }
    }

    private MockHttpServletRequest loginRequest() {
        return loginRequest("10.0.0.5");
    }

    private MockHttpServletRequest loginRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(ip);
        return request;
    }
}
