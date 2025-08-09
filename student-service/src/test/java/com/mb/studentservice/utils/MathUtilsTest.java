package com.mb.studentservice.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MathUtilsTest {

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor;

    @Test
    void test_squareLong() {
        MathUtils mockMathUtils = mock(MathUtils.class);

        when(mockMathUtils.squareLong(10L)).thenReturn(100L);

        assertEquals(100L, mockMathUtils.squareLong(10L));

        verify(mockMathUtils).squareLong(longArgumentCaptor.capture());

        assertEquals(10L, (long) longArgumentCaptor.getValue());
    }

    @Test
    void test_MathUtils() {
        MathUtils mockMathUtils = mock(MathUtils.class);
        when(mockMathUtils.add(20, 20)).thenReturn(40);
        when(mockMathUtils.isInteger(anyString())).thenReturn(true);

        ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals(40, mockMathUtils.add(20, 20));
        assertTrue(mockMathUtils.isInteger("100"));
        assertTrue(mockMathUtils.isInteger("99999"));

        verify(mockMathUtils).add(integerArgumentCaptor.capture(), integerArgumentCaptor.capture());
        List<Integer> allValues = integerArgumentCaptor.getAllValues();
        assertEquals(List.of(20, 20), allValues);

        verify(mockMathUtils, times(2)).isInteger(stringArgumentCaptor.capture());
        List<String> allStringValues = stringArgumentCaptor.getAllValues();
        assertEquals(List.of("100", "99999"), allStringValues);
    }

}
