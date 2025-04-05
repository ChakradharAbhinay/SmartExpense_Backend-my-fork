package com.teamfour.smartexpense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfour.smartexpense.dto.CategoryDTO;
import com.teamfour.smartexpense.security.JwtUtil;
import com.teamfour.smartexpense.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/categories returns all categories")
    void testGetAllCategories() throws Exception {
        var cat1 = CategoryDTO.builder().id(1L).name("Food").build();
        var cat2 = CategoryDTO.builder().id(2L).name("Transport").build();

        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} returns category if found")
    void testGetCategoryById_found() throws Exception {
        var category = CategoryDTO.builder().id(1L).name("Utilities").build();

        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Utilities"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} returns 404 if not found")
    void testGetCategoryById_notFound() throws Exception {
        when(categoryService.getCategoryById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/categories creates category")
    void testCreateCategory() throws Exception {
        var request = CategoryDTO.builder().name("Travel").type("EXPENSE").build();
        var response = CategoryDTO.builder().id(3L).name("Travel").type("EXPENSE").build();

        when(categoryService.createCategory(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Travel"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} updates category if found")
    void testUpdateCategory_found() throws Exception {
        var request = CategoryDTO.builder().name("Updated").color("#123456").build();
        var updated = CategoryDTO.builder().id(1L).name("Updated").color("#123456").build();

        when(categoryService.updateCategory(1L, request)).thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.color").value("#123456"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} returns 404 if category not found")
    void testUpdateCategory_notFound() throws Exception {
        var request = CategoryDTO.builder().name("DoesNotExist").build();

        when(categoryService.updateCategory(99L, request)).thenReturn(null);

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} returns 204 if deleted")
    void testDeleteCategory_found() throws Exception {
        when(categoryService.deleteCategory(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} returns 404 if not found")
    void testDeleteCategory_notFound() throws Exception {
        when(categoryService.deleteCategory(404L)).thenReturn(false);

        mockMvc.perform(delete("/api/categories/404"))
                .andExpect(status().isNotFound());
    }
}
