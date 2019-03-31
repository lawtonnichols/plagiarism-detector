CREATE TABLE top5 (a text, alang text, b text, blang text, score real, primary key (a, b));
CREATE INDEX top5_score ON top5 (score);

INSERT INTO top5 SELECT s.* FROM results s WHERE s.b IN (
	SELECT t.b FROM results t WHERE s.alang != s.blang AND s.a = t.a ORDER BY t.score DESC LIMIT 5
);
