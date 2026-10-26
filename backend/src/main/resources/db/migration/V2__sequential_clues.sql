-- Clues now unlock one per wrong guess instead of being revealed by the player.
-- Stored clues are rebuilt in the new order (details, writer, director, top billing, tagline, plot);
-- the single "people" clue is split up and the characters' names are dropped.
UPDATE movie m
SET clues = jsonb_build_array(
    jsonb_build_object('id', 'metadata', 'type', 'METADATA', 'order', 1, 'content', old.metadata -> 'content'),
    jsonb_build_object('id', 'writer', 'type', 'WRITER', 'order', 2, 'content',
        jsonb_build_object('names', jsonb_path_query_array(old.people, '$[*] ? (@.role == "WRITER").name'))),
    jsonb_build_object('id', 'director', 'type', 'DIRECTOR', 'order', 3, 'content',
        jsonb_build_object('names', jsonb_path_query_array(old.people, '$[*] ? (@.role == "DIRECTOR").name'))),
    jsonb_build_object('id', 'cast', 'type', 'CAST', 'order', 4, 'content',
        jsonb_build_object('names', jsonb_path_query_array(old.people, '$[*] ? (@.role == "CAST").name'))),
    jsonb_build_object('id', 'tagline', 'type', 'TAGLINE', 'order', 5, 'content', old.tagline -> 'content'),
    jsonb_build_object('id', 'plot', 'type', 'PLOT', 'order', 6, 'content', old.plot -> 'content')
)
FROM (
    SELECT id,
           (SELECT c FROM jsonb_array_elements(clues) c WHERE c ->> 'id' = 'metadata') AS metadata,
           (SELECT c FROM jsonb_array_elements(clues) c WHERE c ->> 'id' = 'tagline') AS tagline,
           (SELECT c FROM jsonb_array_elements(clues) c WHERE c ->> 'id' = 'plot') AS plot,
           (SELECT c -> 'content' -> 'people' FROM jsonb_array_elements(clues) c WHERE c ->> 'id' = 'people') AS people
    FROM movie
) old
WHERE old.id = m.id AND old.people IS NOT NULL;

DROP TABLE revealed_clue;
