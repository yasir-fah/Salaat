package com.finalproject.tuwaiqfinal.DTOout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewFeedbackDTO {

    private String summary;
    private String generalImpression;
    private List<String> positivePoints;
    private List<String> negativePoints;
    private List<String> recommendations;


}
