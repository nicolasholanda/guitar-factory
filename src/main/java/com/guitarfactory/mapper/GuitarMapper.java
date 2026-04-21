package com.guitarfactory.mapper;

import com.guitarfactory.domain.entity.Guitar;
import com.guitarfactory.domain.entity.GuitarModel;
import com.guitarfactory.domain.entity.GuitarSpec;
import com.guitarfactory.dto.GuitarModelDto;
import com.guitarfactory.dto.GuitarResponse;
import com.guitarfactory.dto.GuitarSpecDto;
import com.guitarfactory.dto.GuitarSpecRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GuitarMapper {

    GuitarModelDto toModelDto(GuitarModel model);

    @Mapping(source = "model.name", target = "modelName")
    @Mapping(source = "spec", target = "spec")
    GuitarResponse toResponse(Guitar guitar);

    GuitarSpecDto toSpecDto(GuitarSpec spec);

    GuitarSpec toSpec(GuitarSpecRequest request);
}
