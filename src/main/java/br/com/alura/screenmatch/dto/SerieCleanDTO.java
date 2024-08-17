package br.com.alura.screenmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SerieCleanDTO {
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;

}
