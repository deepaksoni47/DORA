import 'package:flutter/material.dart';

void main() {
  runApp(const DoraApp());
}

class DoraApp extends StatelessWidget {
  const DoraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DORA Mobile',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1F6F5F),
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xFFF6F1E8),
        useMaterial3: true,
      ),
      home: const SearchHomePage(),
    );
  }
}

class SearchHomePage extends StatelessWidget {
  const SearchHomePage({super.key});

  static const List<String> categories = <String>[
    'Research Papers',
    'Tutorials',
    'Videos',
    'Articles',
    'Source Code',
  ];

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.78),
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(
                    color: const Color(0xFF483422).withValues(alpha: 0.12),
                  ),
                  boxShadow: const <BoxShadow>[
                    BoxShadow(
                      color: Color(0x143D2A15),
                      blurRadius: 24,
                      offset: Offset(0, 12),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Text(
                      'Academic Meta Search',
                      style: theme.textTheme.labelLarge?.copyWith(
                        color: const Color(0xFF13463C),
                        letterSpacing: 1.1,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'Explore learning resources from one place.',
                      style: theme.textTheme.displaySmall?.copyWith(
                        color: const Color(0xFF2F2419),
                        fontWeight: FontWeight.w700,
                        height: 1.0,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'DORA brings together papers, tutorials, videos, articles, '
                      'and code to support students and researchers in one search experience.',
                      style: theme.textTheme.bodyLarge?.copyWith(
                        color: const Color(0xFF6F5B47),
                        height: 1.6,
                      ),
                    ),
                    const SizedBox(height: 24),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 14,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(18),
                        border: Border.all(
                          color: const Color(0xFF483422).withValues(alpha: 0.08),
                        ),
                      ),
                      child: Row(
                        children: <Widget>[
                          const Icon(Icons.search, color: Color(0xFF1F6F5F)),
                          const SizedBox(width: 12),
                          const Expanded(
                            child: TextField(
                              decoration: InputDecoration(
                                hintText: 'Search machine learning, DBMS, OS...',
                                border: InputBorder.none,
                                isDense: true,
                              ),
                            ),
                          ),
                          FilledButton(
                            onPressed: () {},
                            style: FilledButton.styleFrom(
                              backgroundColor: const Color(0xFF1F6F5F),
                              foregroundColor: const Color(0xFFFFFAF3),
                            ),
                            child: const Text('Search'),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              Text(
                'Categories',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: const Color(0xFF2F2419),
                ),
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 10,
                runSpacing: 10,
                children: categories
                    .map(
                      (String category) => Chip(
                        label: Text(category),
                        backgroundColor: const Color(0xFFD59B43).withValues(
                          alpha: 0.12,
                        ),
                        side: BorderSide(
                          color: const Color(0xFFD59B43).withValues(alpha: 0.18),
                        ),
                        labelStyle: const TextStyle(color: Color(0xFF13463C)),
                      ),
                    )
                    .toList(),
              ),
              const SizedBox(height: 24),
              Text(
                'Planned mobile features',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 12),
              const FeatureCard(
                icon: Icons.filter_alt_outlined,
                title: 'Smart filters',
                description: 'Refine results by type, source, and publication year.',
              ),
              const SizedBox(height: 12),
              const FeatureCard(
                icon: Icons.bookmark_border,
                title: 'Saved resources',
                description: 'Bookmark useful content for future reading and revision.',
              ),
              const SizedBox(height: 12),
              const FeatureCard(
                icon: Icons.sync_alt,
                title: 'Unified results',
                description: 'Access papers, videos, tutorials, and repositories together.',
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class FeatureCard extends StatelessWidget {
  const FeatureCard({
    required this.icon,
    required this.title,
    required this.description,
    super.key,
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(
          color: const Color(0xFF483422).withValues(alpha: 0.08),
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: const Color(0xFF1F6F5F).withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: const Color(0xFF1F6F5F)),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  title,
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: const Color(0xFF2F2419),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  description,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: const Color(0xFF6F5B47),
                    height: 1.5,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
