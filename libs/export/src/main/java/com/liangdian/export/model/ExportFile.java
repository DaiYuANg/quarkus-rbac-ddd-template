package com.liangdian.export.model;

public record ExportFile(String fileName, String contentType, byte[] content) {}
