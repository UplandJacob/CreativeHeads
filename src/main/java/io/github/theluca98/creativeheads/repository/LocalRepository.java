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
import io.github.theluca98.creativeheads.model.HeadCategory;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LocalRepository {

    public static final Pattern ESCAPE = Pattern.compile("[^\\w\\s]");
    public static final TemporalAmount UPDATE_TIMEOUT = Duration.ofDays(2);

    private final CreativeHeads plugin;
    private final RemoteRepository remote;
    private final QueryRunner db;

    @SneakyThrows
    public LocalRepository(CreativeHeads plugin) {
        this.plugin = plugin;
        this.remote = new RemoteRepository(plugin);
        this.db = createQueryRunner();
    }

    public CompletableFuture<Void> updateDatabaseAsync() {
        return CompletableFuture.runAsync(this::updateDatabase);
    }

    public CompletableFuture<List<CustomHead>> executeSearchAsync(String query) {
        return CompletableFuture.supplyAsync(() -> executeSearch(query));
    }

    public CompletableFuture<List<CustomHead>> findByCategoryAsync(HeadCategory category) {
        return CompletableFuture.supplyAsync(() -> findByCategory(category));
    }

    private QueryRunner createQueryRunner() {
        plugin.getLogger().info("Opening local database...");
        var dataSource = new SQLiteDataSource();
        dataSource.setUrl(JDBC.PREFIX + plugin.getFilePath("local.db"));
        return new QueryRunner(dataSource);
    }

    @SneakyThrows
    private void updateDatabase() {
        if (needsUpdate()) {
            try (var heads = remote.downloadData()) {
                initializeDatabase();
                saveData(heads);
            } catch(Exception e) {
                plugin.getLogger().warn("Failed to update from remote database: "+e.getMessage());
                break;
            }
            plugin.getLogger().info("The database is now up to date.");
        } else {
            plugin.getLogger().info("The database was updated recently, skipping.");
        }
    }

    private boolean needsUpdate() throws IOException {
        var timestampFile = plugin.getFilePath(".lastupdate");
        if (Files.exists(timestampFile)) {
            var timestamp = Files.readString(timestampFile).trim();
            return Instant.parse(timestamp).plus(UPDATE_TIMEOUT).isBefore(Instant.now());
        } else {
            return true;
        }
    }

    @SneakyThrows
    private void initializeDatabase() {
        var currentTime = Instant.now(Clock.tickSeconds(ZoneId.systemDefault()));
        plugin.getLogger().info("Creating new local database...");
        Files.deleteIfExists(plugin.getFilePath("local.db"));
        db.update("CREATE VIRTUAL TABLE heads USING fts5 (name, category, hash UNINDEXED, tags)");
        Files.writeString(plugin.getFilePath(".lastupdate"), currentTime.toString());
    }

    @SneakyThrows
    private void saveData(Stream<CustomHead> heads) {
        plugin.getLogger().info("Saving data...");
        var data = heads.map(CustomHead::toRowArray).toArray(String[][]::new);
        try (var connection = db.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            db.batch(connection, "INSERT INTO heads VALUES (?, ?, ?, ?)", data);
            connection.commit();
        }
    }

    @SneakyThrows
    private List<CustomHead> executeSearch(String query) {
        // TODO Improve this...
        var ftsQuery = ESCAPE.matcher(query).replaceAll("");
        if (ftsQuery.length() == 0) {
            return Collections.emptyList();
        }
        return db.query("SELECT * FROM heads WHERE name MATCH ? ORDER BY rank", CustomHead.HANDLER, ftsQuery);
    }

    @SneakyThrows
    private List<CustomHead> findByCategory(HeadCategory category) {
        return db.query("SELECT * FROM heads WHERE category = ? ORDER BY name ASC", CustomHead.HANDLER, category.getId());
    }

}
