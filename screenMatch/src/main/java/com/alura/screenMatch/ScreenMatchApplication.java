package com.alura.screenMatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alura.screenMatch.model.DadosSerie;
import com.alura.screenMatch.service.ConsumoApi;
import com.alura.screenMatch.service.ConverterDados;

@SpringBootApplication
public class ScreenMatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenMatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var apiKey = "a1239a9c";
		var json = ConsumoApi.obterDados("https://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey);
		
		ConverterDados conversor = new ConverterDados();
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);
	}

}
