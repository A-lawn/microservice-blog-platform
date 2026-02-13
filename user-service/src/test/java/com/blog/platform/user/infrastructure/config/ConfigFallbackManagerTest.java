package com.blog.platform.user.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigFallbackManagerTest {
    
    @Mock
    private Environment environment;
    
    private ConfigFallbackManager configFallbackManager;
    
    @BeforeEach
    void setUp() {
        configFallbackManager = new ConfigFallbackManager(environment);
    }
    
    @Test
    void shouldReturnDefaultValueWhenConfigNotAvailable() {
        // Given
        when(environment.getProperty("test.key")).thenReturn(null);
        
        // When
        String result = configFallbackManager.getConfigValue("test.key", "default");
        
        // Then
        assertEquals("default", result);
    }
    
    @Test
    void shouldReturnConfigValueWhenAvailable() {
        // Given
        when(environment.getProperty("test.key")).thenReturn("config-value");
        
        // When
        String result = configFallbackManager.getConfigValue("test.key", "default");
        
        // Then
        assertEquals("config-value", result);
    }
    
    @Test
    void shouldReturnCachedValueWhenConfigCenterUnavailable() {
        // Given
        when(environment.getProperty("test.key")).thenThrow(new RuntimeException("Config center unavailable"));
        
        // When - first call to cache the value
        configFallbackManager.getConfigValue("test.key", "default");
        
        // Then - should return default since no cached value exists
        String result = configFallbackManager.getConfigValue("test.key", "default");
        assertEquals("default", result);
        assertFalse(configFallbackManager.isConfigCenterAvailable());
    }
    
    @Test
    void shouldConvertIntegerConfigValue() {
        // Given
        when(environment.getProperty("test.int")).thenReturn("123");
        
        // When
        int result = configFallbackManager.getIntConfigValue("test.int", 456);
        
        // Then
        assertEquals(123, result);
    }
    
    @Test
    void shouldReturnDefaultForInvalidIntegerConfig() {
        // Given
        when(environment.getProperty("test.int")).thenReturn("invalid");
        
        // When
        int result = configFallbackManager.getIntConfigValue("test.int", 456);
        
        // Then
        assertEquals(456, result);
    }
    
    @Test
    void shouldConvertBooleanConfigValue() {
        // Given
        when(environment.getProperty("test.bool")).thenReturn("true");
        
        // When
        boolean result = configFallbackManager.getBooleanConfigValue("test.bool", false);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void shouldConvertLongConfigValue() {
        // Given
        when(environment.getProperty("test.long")).thenReturn("123456789");
        
        // When
        long result = configFallbackManager.getLongConfigValue("test.long", 987654321L);
        
        // Then
        assertEquals(123456789L, result);
    }
}