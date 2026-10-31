package com.mb.modulith.email.client.emailresource.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "dataset")
public class DataSetResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "emails")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<EmailDto> emails = new ArrayList<>();

    @JacksonXmlProperty(localName = "resources")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ResourceDto> resources = new ArrayList<>();
}
