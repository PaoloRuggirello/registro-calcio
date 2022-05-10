package com.elis.registrocalcio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoriesDTO {

    public List<String> categories;

    public CategoriesDTO() {
    }

    public CategoriesDTO(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("categories", categories)
                .toString();
    }
}

