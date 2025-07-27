-- 创建AI模型表
CREATE TABLE ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    description TEXT COMMENT '模型描述',
    icon VARCHAR(255) COMMENT '模型图标URL',
    category VARCHAR(50) NOT NULL COMMENT '模型分类：recommend/office/writing/life/education/entertainment',
    prompt TEXT NOT NULL COMMENT '模型的系统提示词',
    source VARCHAR(100) COMMENT '模型来源/作者',
    tags JSON COMMENT '模型标签（JSON数组）',
    is_official TINYINT(1) DEFAULT 0 COMMENT '是否官方模型：0-否，1-是',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重，数值越大越靠前',
    usage_count BIGINT DEFAULT 0 COMMENT '使用次数',
    creator_id BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    
    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_is_official (is_official),
    INDEX idx_sort_order (sort_order),
    INDEX idx_creator_id (creator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型表';

-- 创建模型分类表
CREATE TABLE ai_model_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_key VARCHAR(50) NOT NULL UNIQUE COMMENT '分类键值',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    icon VARCHAR(255) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_sort_order (sort_order),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型分类表';

-- 插入默认分类数据
INSERT INTO ai_model_category (category_key, category_name, description, sort_order) VALUES
('recommend', '官方推荐', '官方精选推荐的优质AI模型', 100),
('office', '办公提效', '提升办公效率的AI助手', 90),
('writing', '辅助写作', '帮助写作创作的AI工具', 80),
('life', '生活实用', '解决生活问题的AI助手', 70),
('education', '教育学习', '教育和学习相关的AI模型', 60),
('entertainment', '娱乐休闲', '娱乐和休闲相关的AI助手', 50);

-- 插入示例AI模型数据
INSERT INTO ai_model (name, description, icon, category, prompt, source, tags, is_official, is_active, sort_order, creator_id) VALUES
('MyAI 智能助手', '全能型AI助手，可以回答各种问题，协助完成多种任务', 'MyAIlogin.svg', 'recommend',
'你是MyAI智能助手，一个友好、专业、有帮助的AI助手。你可以回答用户的各种问题，提供准确的信息和建议。请用简洁明了的语言回答，保持友好和专业的语调。',
'MyAI官方', '["智能助手", "全能型", "问答"]', 1, 1, 100, 1),

('代码助手', '专业的编程助手，帮助你编写、调试和优化代码', 'code-assistant.svg', 'office',
'你是一个专业的编程助手，精通多种编程语言和开发技术。你可以帮助用户：1. 编写高质量的代码 2. 调试和修复代码问题 3. 优化代码性能 4. 解释代码逻辑 5. 提供最佳实践建议。请提供清晰、可执行的代码示例，并解释关键概念。',
'MyAI官方', '["编程", "代码", "开发"]', 1, 1, 90, 1),

('文案创作师', '专业的文案创作助手，帮助你创作各种类型的文案', 'writer.svg', 'writing',
'你是一个专业的文案创作师，擅长创作各种类型的文案内容。你可以帮助用户：1. 撰写营销文案 2. 创作广告标语 3. 编写产品描述 4. 制作社交媒体内容 5. 撰写邮件和通知。请确保文案具有吸引力、准确性和适当的语调。',
'MyAI官方', '["文案", "创作", "营销"]', 1, 1, 85, 1),

('学习导师', '个性化学习助手，帮助你制定学习计划和解答学习问题', 'teacher.svg', 'education',
'你是一个耐心的学习导师，专门帮助学生学习和成长。你可以：1. 解答各学科问题 2. 制定个性化学习计划 3. 提供学习方法建议 4. 帮助理解复杂概念 5. 激励学习动力。请用通俗易懂的语言解释，循序渐进地引导学习。',
'MyAI官方', '["教育", "学习", "导师"]', 1, 1, 80, 1),

('生活助手', '贴心的生活助手，帮助你解决日常生活中的各种问题', 'life-assistant.svg', 'life',
'你是一个贴心的生活助手，熟悉日常生活的方方面面。你可以帮助用户：1. 提供生活小贴士 2. 解答健康养生问题 3. 推荐美食和菜谱 4. 协助旅行规划 5. 解决家居问题。请提供实用、安全的建议，关注用户的生活质量。',
'MyAI官方', '["生活", "实用", "贴心"]', 1, 1, 75, 1),

('翻译专家', '专业的多语言翻译助手，支持多种语言互译', 'translator.svg', 'office',
'你是一个专业的翻译专家，精通多种语言的翻译工作。你可以：1. 提供准确的文本翻译 2. 解释语言文化差异 3. 协助语言学习 4. 润色翻译文本 5. 提供本地化建议。请确保翻译的准确性和文化适应性。',
'MyAI官方', '["翻译", "多语言", "专业"]', 1, 1, 70, 1);
