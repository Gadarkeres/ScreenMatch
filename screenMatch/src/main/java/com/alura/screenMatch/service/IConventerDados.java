package com.alura.screenMatch.service;

public interface IConventerDados {
  <T> T obterDados(String json, Class<T> classe);
}
