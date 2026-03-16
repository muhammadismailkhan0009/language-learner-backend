update flashcard_review
set review_log_json = jsonb_build_array(review_log_json)
where review_log_json is not null
  and jsonb_typeof(review_log_json) = 'object';
