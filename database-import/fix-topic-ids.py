"""
Fix topic_id values in questiondb-sample-data.sql
Remap discontinuous topic IDs to continuous 1-15
"""
import re

# Topic ID remapping
TOPIC_MAPPING = {
    23: 15, 22: 14, 21: 13,
    18: 12, 17: 11, 16: 10,
    13: 9, 12: 8, 11: 7,
    8: 6, 7: 5, 6: 4
}

def fix_topic_ids(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern to match question INSERT rows: (user_id, topic_id, field_id, level_id, question_type_id, ...)
    # The pattern looks for lines starting with ( followed by numbers separated by commas
    pattern = r'\((\d+), (\d+), (\d+), (\d+), (\d+),'
    
    def replace_topic(match):
        user_id = match.group(1)
        topic_id = int(match.group(2))
        field_id = match.group(3)
        level_id = match.group(4)
        question_type_id = match.group(5)
        
        # Remap topic_id if needed
        new_topic_id = TOPIC_MAPPING.get(topic_id, topic_id)
        
        return f'({user_id}, {new_topic_id}, {field_id}, {level_id}, {question_type_id},'
    
    # Replace all matches
    fixed_content = re.sub(pattern, replace_topic, content)
    
    # Write to output file
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(fixed_content)
    
    print(f"✓ Fixed topic IDs in {output_file}")
    
    # Count topics for verification
    matches = re.findall(pattern, fixed_content)
    if matches:
        topic_counts = {}
        for match in matches:
            topic_id = int(match[1])
            topic_counts[topic_id] = topic_counts.get(topic_id, 0) + 1
        
        print(f"\nTopic distribution:")
        for topic_id in sorted(topic_counts.keys()):
            print(f"  Topic {topic_id}: {topic_counts[topic_id]} questions")
        
        print(f"\nTotal questions: {len(matches)}")
        print(f"Topic range: {min(topic_counts.keys())} to {max(topic_counts.keys())}")

if __name__ == '__main__':
    input_file = 'questiondb-sample-data.sql'
    output_file = 'questiondb-sample-data.sql'
    
    # Create backup first
    import shutil
    backup_file = 'questiondb-sample-data.sql.bak'
    shutil.copy(input_file, backup_file)
    print(f"✓ Backup created: {backup_file}")
    
    fix_topic_ids(input_file, output_file)
