/*
 * CreativeHeads
 * Copyright (C) 2022 Luca <https://github.com/TheLuca98>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.theluca98.creativeheads.repository;

import io.github.theluca98.creativeheads.CreativeHeads;
import io.github.theluca98.creativeheads.model.CustomHead;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.stream.Stream;

public class RemoteRepository {

    private static final String LIST_URL = "https://cdn.jsdelivr.net/gh/TheLuca98/MinecraftHeads@master/heads.csv";

    private final CreativeHeads plugin;
    private final HttpClient client;
    private final CSVFormat csvFormat;

    public RemoteRepository(CreativeHeads plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
        this.csvFormat = CSVFormat.DEFAULT.builder()
            .setSkipHeaderRecord(true)
            .setHeader()
            .build();
    }

    @SneakyThrows
    public Stream<CustomHead> downloadData() {
        plugin.getLogger().info("Downloading latest head database from remote server...");
        var request = HttpRequest.newBuilder(URI.create(LIST_URL)).build();
        var response = client.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            plugin.getLogger().info("Parsing CSV...");
            return CSVParser.parse(response.body(), csvFormat)
                .stream()
                .map(CustomHead::fromCSV);
        } else {
            throw new IOException("CDN responded with status code " + response.statusCode());
        }
    }

}
